require "linguist/samples"
require "CSV"

module Linguist

	#Loads content and finds the linguist tags.
	class TagsFinder

		#load_map
		def self.load_map(file)
			#Make maps
			$token_map = Hash.new()
			#Read file
			File.open(file, "r") do |f|
				f.each_line do |line|
					array = line.split(",")
					if array.length == 2
						#get the database tag
						database_tag = array[1].strip
						if database_tag != ''
							#map database tag to linguist language
							linguist_tag = array[0].strip
							$token_map["#{database_tag}"] = linguist_tag
						end
					end
				end
			end
		end


		#load languages file
		def self.load_languages()
			languages = []
			File.absolute_path("‎⁨languages.txt", "r") do |f|
				f.each_line do |line|
					file = line.strip
					if !($token_map.values.include? file)
						next
					end
					languages << Linguist::Language.find_by_name(file)
				end
			end
				$language_names = languages.map(&:name)
		end

		#Finds linguist tags
		def self.find_tags(sample_ids)
			
			csv_new = File.open("linguist.csv", 'w')
			#Process each id
			sample_ids.each { |id|
				code = get_code_snippets(id)
				for index in 0 ... code.size
					csv_new.puts("#{id}\t#{get_linguist_results(code[index][1])}")	#(code[index][1])}")
				end
			}
			csv_new.close()
		end

		#Let linguist read the code
		def self.get_linguist_results(code)
			results = Linguist::Classifier.classify(Samples.cache, code, $language_names).map do |name, _|
				name
			end
			return results[0]
		end

		#Get ID - read from the file
		def self.get_id()			
			postid = []
			path = "user_tags.csv"
			csv = CSV.foreach(path, :headers => true, :quote_char => "|") do |row|
				postid << row[0]
			end

			return postid
		end


		#Get code snippets - read from the file
		def self.get_code_snippets(sample_id)	
			code = []
			path = "user_tags.csv"
			csv_text = File.path(path)
			csv = CSV.foreach(csv_text, :headers => true, :quote_char => "|") do |row|
				id = row[0]
				case when id == sample_id
					code << row[2]
				end
			end
			return code
		end

		begin
			puts Time.new
			load_map(ARGV[0])
			load_languages()
			post_id = get_id()
			find_tags(post_id)
			puts Time.new
		end
	end
end
